package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pantrybuddy.data.local.dao.PantryDao;
import com.example.pantrybuddy.data.local.dao.RecipeDao;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.data.repository.PantryRepository;
import com.example.pantrybuddy.data.repository.RecipeRepository;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

// Tests repository and viewmodel interactions
public class RepositoryViewModelTest {

    private FakePantryDao fakePantryDao;
    private FakeRecipeDao fakeRecipeDao;
    private ExecutorService directExecutor;

    @Before
    public void setUp() {
        // Runs LiveData operations synchronously in unit tests
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postToMainThread(Runnable runnable) {
                runnable.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        });

        // Synchronous executor for instant execution in tests
        directExecutor = new AbstractExecutorService() {
            private boolean isShutdown = false;

            @Override
            public void shutdown() {
                isShutdown = true;
            }

            @Override
            public List<Runnable> shutdownNow() {
                isShutdown = true;
                return Collections.emptyList();
            }

            @Override
            public boolean isShutdown() {
                return isShutdown;
            }

            @Override
            public boolean isTerminated() {
                return isShutdown;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) {
                return true;
            }

            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };

        fakePantryDao = new FakePantryDao();
        fakeRecipeDao = new FakeRecipeDao();
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
        directExecutor.shutdown();
    }

    @Test
    public void testPantryRepository_insertAndDeductIngredient() {
        PantryRepository repo = new PantryRepository(fakePantryDao, directExecutor);

        // Add 4 eggs
        PantryItem eggs = new PantryItem("Eggs", "", 4.0, "pcs", null, "Dairy & Eggs", System.currentTimeMillis());
        repo.insert(eggs);

        // Name should be normalized to egg
        assertEquals(1, fakePantryDao.itemsList.size());
        assertEquals("egg", fakePantryDao.itemsList.get(0).getNormalizedName());

        // Deduct 2 eggs
        repo.deductIngredient("egg", 2.0, "pcs");
        assertEquals(2.0, fakePantryDao.itemsList.get(0).getQuantity(), 0.001);

        // Deduct remaining 2 eggs (should delete item from list)
        repo.deductIngredient("egg", 2.0, "pcs");
        assertEquals(0, fakePantryDao.itemsList.size());
    }

    @Test
    public void testRecipeRepository_matchingEngineEmitsResults() {
        long now = System.currentTimeMillis();
        // Setup recipe: 2 eggs + 100g spinach
        Recipe recipe = new Recipe("Egg & Spinach Skillet", "Quick breakfast", 10, 1, "[]", "", "Easy");
        recipe.setRecipeId(1L);

        List<RecipeIngredient> ingredients = Arrays.asList(
                new RecipeIngredient(1L, "Eggs", "egg", 2.0, "pcs"),
                new RecipeIngredient(1L, "Spinach", "spinach", 100.0, "g")
        );
        RecipeWithIngredients rwi = new RecipeWithIngredients();
        rwi.setRecipe(recipe);
        rwi.setIngredients(ingredients);
        fakeRecipeDao.setRecipes(Collections.singletonList(rwi));

        // Start with only eggs in pantry (missing spinach)
        fakePantryDao.setItems(Collections.singletonList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now)
        ));

        RecipeRepository repo = new RecipeRepository(fakeRecipeDao, fakePantryDao, directExecutor);

        // LiveData observer
        repo.getCookableRecipes().observeForever(list -> {});
        repo.getAlmostThereRecipes().observeForever(list -> {});
        repo.getZeroMatchState().observeForever(b -> {});

        repo.runMatching();

        // Should have 0 strictly cookable, 1 in almost there (needs spinach), and zeroMatch is true
        assertNotNull(repo.getCookableRecipes().getValue());
        assertEquals(0, repo.getCookableRecipes().getValue().size());
        assertEquals(1, repo.getAlmostThereRecipes().getValue().size());
        assertEquals(true, repo.getZeroMatchState().getValue());

        // Now add spinach to pantry
        fakePantryDao.setItems(Arrays.asList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Spinach", "spinach", 150.0, "g", null, "Vegetables", now)
        ));

        repo.runMatching();

        // Now recipe should be strictly cookable
        assertEquals(1, repo.getCookableRecipes().getValue().size());
        assertEquals(0, repo.getAlmostThereRecipes().getValue().size());
        assertEquals(false, repo.getZeroMatchState().getValue());
    }

    @Test
    public void testPantryViewModel_filtersAndSorting() {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrow = cal.getTimeInMillis();

        List<PantryItem> list = Arrays.asList(
                new PantryItem("Tomato", "tomato", 200.0, "g", tomorrow, "Vegetables", now - 1000),
                new PantryItem("Milk", "milk", 1.0, "l", null, "Dairy & Eggs", now - 2000),
                new PantryItem("Carrot", "carrot", 300.0, "g", null, "Vegetables", now - 3000)
        );
        fakePantryDao.setItems(list);

        PantryRepository repo = new PantryRepository(fakePantryDao, directExecutor);
        PantryViewModel vm = new PantryViewModel(new Application(), repo);

        vm.getFilteredPantryItems().observeForever(items -> {});
        vm.getExpiringSoonItems().observeForever(items -> {});

        // Initial list has 3 items
        assertEquals(3, vm.getFilteredPantryItems().getValue().size());

        // Filter by category: Vegetables
        vm.setCategoryFilter("Vegetables");
        assertEquals(2, vm.getFilteredPantryItems().getValue().size());

        // Search query: carrot
        vm.setSearchQuery("carrot");
        assertEquals(1, vm.getFilteredPantryItems().getValue().size());
        assertEquals("Carrot", vm.getFilteredPantryItems().getValue().get(0).getName());

        // Reset search and category
        vm.setSearchQuery("");
        vm.setCategoryFilter("All");
        assertEquals(3, vm.getFilteredPantryItems().getValue().size());

        // Sort by name
        vm.setSortOrder(PantryViewModel.SortOrder.NAME_ASC);
        List<PantryItem> sorted = vm.getFilteredPantryItems().getValue();
        assertEquals("Carrot", sorted.get(0).getName());
        assertEquals("Milk", sorted.get(1).getName());
        assertEquals("Tomato", sorted.get(2).getName());

        // Expiring soon should have 1 item (Tomato expiring tomorrow)
        assertEquals(1, vm.getExpiringSoonItems().getValue().size());
        assertEquals("Tomato", vm.getExpiringSoonItems().getValue().get(0).getName());
    }

    @Test
    public void testRecipeViewModel_cookTimeFilter() {
        long now = System.currentTimeMillis();
        Recipe r1 = new Recipe("Quick Toast", "", 5, 1, "[]", "", "Easy");
        r1.setRecipeId(1L);
        Recipe r2 = new Recipe("Slow Stew", "", 45, 4, "[]", "", "Medium");
        r2.setRecipeId(2L);

        RecipeWithIngredients rwi1 = new RecipeWithIngredients();
        rwi1.setRecipe(r1);
        rwi1.setIngredients(Collections.singletonList(
                new RecipeIngredient(1L, "Bread", "bread", 2.0, "pcs")
        ));

        RecipeWithIngredients rwi2 = new RecipeWithIngredients();
        rwi2.setRecipe(r2);
        rwi2.setIngredients(Collections.singletonList(
                new RecipeIngredient(2L, "Bread", "bread", 2.0, "pcs")
        ));

        fakeRecipeDao.setRecipes(Arrays.asList(rwi1, rwi2));
        fakePantryDao.setItems(Collections.singletonList(
                new PantryItem("Bread", "bread", 10.0, "pcs", null, "Staples", now)
        ));

        RecipeRepository repo = new RecipeRepository(fakeRecipeDao, fakePantryDao, directExecutor);
        RecipeViewModel vm = new RecipeViewModel(new Application(), repo);

        vm.getCookableRecipes().observeForever(l -> {});
        vm.getFilteredCookableRecipes().observeForever(l -> {});

        repo.runMatching();

        assertEquals(2, vm.getCookableRecipes().getValue().size());

        // Filter recipes to under 15 minutes
        vm.setMaxCookTimeFilter(15);
        assertEquals(1, vm.getFilteredCookableRecipes().getValue().size());
        assertEquals("Quick Toast", vm.getFilteredCookableRecipes().getValue().get(0).getRecipe().getTitle());
    }

    @Test
    public void testRecipeViewModelAlmostThereAndExpiringFilters() {
        long now = System.currentTimeMillis();
        long tomorrow = now + (24L * 60 * 60 * 1000);

        Recipe r1 = new Recipe("Bread Toast", "Quick snack", 5, 1, "[]", "", "Easy");
        r1.setRecipeId(1L);
        RecipeWithIngredients rwi1 = new RecipeWithIngredients();
        rwi1.setRecipe(r1);
        rwi1.setIngredients(Collections.singletonList(
                new RecipeIngredient(1L, "Bread", "bread", 2.0, "pcs")
        ));

        Recipe r2 = new Recipe("Warm Milk", "Warm drink", 5, 1, "[]", "", "Easy");
        r2.setRecipeId(2L);
        RecipeWithIngredients rwi2 = new RecipeWithIngredients();
        rwi2.setRecipe(r2);
        rwi2.setIngredients(Collections.singletonList(
                new RecipeIngredient(2L, "Milk", "milk", 200.0, "ml")
        ));

        Recipe r3 = new Recipe("Butter Toast", "Rich toast", 5, 1, "[]", "", "Easy");
        r3.setRecipeId(3L);
        RecipeWithIngredients rwi3 = new RecipeWithIngredients();
        rwi3.setRecipe(r3);
        rwi3.setIngredients(Arrays.asList(
                new RecipeIngredient(3L, "Bread", "bread", 2.0, "pcs"),
                new RecipeIngredient(3L, "Butter", "butter", 20.0, "g")
        ));

        fakeRecipeDao.setRecipes(Arrays.asList(rwi1, rwi2, rwi3));
        fakePantryDao.setItems(Arrays.asList(
                new PantryItem("Bread", "bread", 10.0, "pcs", null, "Staples", now),
                new PantryItem("Milk", "milk", 500.0, "ml", tomorrow, "Dairy & Eggs", now)
        ));

        RecipeRepository repo = new RecipeRepository(fakeRecipeDao, fakePantryDao, directExecutor);
        RecipeViewModel vm = new RecipeViewModel(new Application(), repo);

        vm.getCookableRecipes().observeForever(l -> {});
        vm.getFilteredCookableRecipes().observeForever(l -> {});
        vm.getAlmostThereRecipes().observeForever(l -> {});
        vm.getFilteredAlmostThereRecipes().observeForever(l -> {});

        repo.runMatching();

        // 2 cookable, 1 almost there
        assertEquals(2, vm.getCookableRecipes().getValue().size());
        assertEquals(1, vm.getAlmostThereRecipes().getValue().size());
        assertEquals("Butter Toast", vm.getAlmostThereRecipes().getValue().get(0).getRecipe().getTitle());

        // Filter only expiring items: only Warm Milk uses milk (expiring tomorrow)
        vm.setOnlyExpiringSoonFilter(true);
        assertEquals(1, vm.getFilteredCookableRecipes().getValue().size());
        assertEquals("Warm Milk", vm.getFilteredCookableRecipes().getValue().get(0).getRecipe().getTitle());

        // Reset expiring filter
        vm.setOnlyExpiringSoonFilter(false);
        assertEquals(2, vm.getFilteredCookableRecipes().getValue().size());

        // Search query filters both lists
        vm.setRecipeSearchQuery("Butter");
        assertEquals(0, vm.getFilteredCookableRecipes().getValue().size());
        assertEquals(1, vm.getFilteredAlmostThereRecipes().getValue().size());
        assertEquals("Butter Toast", vm.getFilteredAlmostThereRecipes().getValue().get(0).getRecipe().getTitle());
    }

    // Fake PantryDao for testing without SQLite device dependencies
    private static class FakePantryDao implements PantryDao {
        final List<PantryItem> itemsList = new ArrayList<>();
        final MutableLiveData<List<PantryItem>> liveList = new MutableLiveData<>(new ArrayList<>());
        final MutableLiveData<Integer> countLive = new MutableLiveData<>(0);

        void setItems(List<PantryItem> items) {
            itemsList.clear();
            itemsList.addAll(items);
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(itemsList.size());
        }

        @Override
        public long insert(PantryItem item) {
            itemsList.add(item);
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(itemsList.size());
            return itemsList.size();
        }

        @Override
        public void insertAll(List<PantryItem> items) {
            itemsList.addAll(items);
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(itemsList.size());
        }

        @Override
        public void update(PantryItem item) {
            liveList.setValue(new ArrayList<>(itemsList));
        }

        @Override
        public void delete(PantryItem item) {
            itemsList.remove(item);
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(itemsList.size());
        }

        @Override
        public void deleteById(long id) {
            itemsList.removeIf(i -> i.getItemId() == id);
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(itemsList.size());
        }

        @Override
        public void deleteAll() {
            itemsList.clear();
            liveList.setValue(new ArrayList<>(itemsList));
            countLive.setValue(0);
        }

        @Override
        public LiveData<List<PantryItem>> getAllPantryItems() {
            return liveList;
        }

        @Override
        public List<PantryItem> getAllPantryItemsSync() {
            return new ArrayList<>(itemsList);
        }

        @Override
        public PantryItem getItemById(long id) {
            for (PantryItem item : itemsList) {
                if (item.getItemId() == id) return item;
            }
            return null;
        }

        @Override
        public PantryItem getItemByNormalizedName(String normalizedName) {
            for (PantryItem item : itemsList) {
                if (item.getNormalizedName().equalsIgnoreCase(normalizedName)) return item;
            }
            return null;
        }

        @Override
        public LiveData<List<PantryItem>> getItemsByCategory(String category) {
            return liveList;
        }

        @Override
        public LiveData<List<PantryItem>> searchItems(String query) {
            return liveList;
        }

        @Override
        public int getItemCount() {
            return itemsList.size();
        }

        @Override
        public LiveData<Integer> getItemCountLive() {
            return countLive;
        }

        @Override
        public LiveData<List<PantryItem>> getItemsExpiringBefore(long thresholdTime) {
            return liveList;
        }
    }

    // Fake RecipeDao for testing
    private static class FakeRecipeDao implements RecipeDao {
        final List<RecipeWithIngredients> recipeList = new ArrayList<>();
        final MutableLiveData<List<RecipeWithIngredients>> liveRecipes = new MutableLiveData<>(new ArrayList<>());

        void setRecipes(List<RecipeWithIngredients> recipes) {
            recipeList.clear();
            recipeList.addAll(recipes);
            liveRecipes.setValue(new ArrayList<>(recipeList));
        }

        @Override
        public long insertRecipe(Recipe recipe) {
            return 1L;
        }

        @Override
        public void insertRecipes(List<Recipe> recipes) {}

        @Override
        public void insertIngredients(List<RecipeIngredient> ingredients) {}

        @Override
        public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
            return liveRecipes;
        }

        @Override
        public List<RecipeWithIngredients> getAllRecipesWithIngredientsSync() {
            return new ArrayList<>(recipeList);
        }

        @Override
        public RecipeWithIngredients getRecipeWithIngredientsById(long recipeId) {
            for (RecipeWithIngredients rwi : recipeList) {
                if (rwi.getRecipe().getRecipeId() == recipeId) return rwi;
            }
            return null;
        }

        @Override
        public LiveData<RecipeWithIngredients> getRecipeWithIngredientsByIdLive(long recipeId) {
            return new MutableLiveData<>(getRecipeWithIngredientsById(recipeId));
        }

        @Override
        public int getRecipeCount() {
            return recipeList.size();
        }

        @Override
        public void deleteAllRecipes() {}

        @Override
        public void deleteAllIngredients() {}
    }
}

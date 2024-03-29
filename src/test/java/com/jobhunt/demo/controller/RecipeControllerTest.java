package com.jobhunt.demo.controller;

import com.jobhunt.demo.client.RecipeFileClient;
import com.jobhunt.demo.exception.NotFoundException;
import com.jobhunt.demo.model.RecipeResponse;
import com.jobhunt.demo.service.*;

import java.util.ArrayDeque;
import java.util.ArrayList;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("integration")
@ExtendWith(MockitoExtension.class)

public class RecipeControllerTest {
    private RecipeService recipeService;
    private FileService fileService;
    private IngredientService ingredientService;
    private RecipeController recipeController;
    private RecipeFileClient recipeFileClient;

    @InjectMocks
    private DataService dataService;

    private static final String fileName = System.getProperty("user.dir") + "/src/test/ingredients.json";

    @BeforeEach
    public void init() {
        LocalFile localFile = new LocalFile();
        localFile.setFileName(fileName);
        fileService = new FileService(new HostedFile(recipeFileClient), localFile);
        recipeService = new RecipeService(fileService);
        ingredientService = new IngredientService(fileService);
        recipeController = new RecipeController(recipeService,ingredientService);
    }

    @Test
    public void testExpiredBreadOneMonth() {
        List<String> ingredients = List.of("Bread");
        dataService.createUnitTestFile(fileName, ingredients, -30, -30);
        
        ArrayDeque<RecipeResponse> arrayDeque = recipeController.getRecipes();
        Assertions.assertEquals(arrayDeque.size(), 3);
    }

    @Test
    public void testLettuceUseByToday() {
        List<String> ingredients = List.of("Lettuce");
        dataService.createUnitTestFile(fileName, ingredients, 0, 0);

        ArrayDeque<RecipeResponse> arrayDeque = recipeController.getRecipes();
        Assertions.assertEquals(arrayDeque.size(), 4);
    }

    @Test
    public void testExpiredByTomorrow() {
        List<String> ingredients = List.of("Spinach", "Cheese", "Bread");
        dataService.createUnitTestFile(fileName, ingredients, 1, 1);

        ArrayDeque<RecipeResponse> arrayDeque = recipeController.getRecipes();
        Assertions.assertEquals(arrayDeque.size(), 2);
    }

    @Test
    public void testExpiredYesterday() {
        List<String> ingredients = List.of("Spinach", "Cheese", "Bread");
        dataService.createUnitTestFile(fileName, ingredients, 1, -1);

        ArrayDeque<RecipeResponse> arrayDeque = recipeController.getRecipes();
        Assertions.assertEquals(arrayDeque.size(), 2);
    }
    @Test
    public void testSortLast() {
        List<String> ingredients = List.of("Hotdog Bun");
        dataService.createUnitTestFile(fileName, ingredients, -1, 0);

        ArrayDeque<RecipeResponse> arrayDeque = recipeController.getRecipes();
        Assertions.assertEquals(arrayDeque.size(), 4);

        RecipeResponse last = arrayDeque.getLast();
        Assertions.assertEquals(last.getTitle(), "Ham and Cheese Toastie");
    }

    @Test
    public void testErrorHandling() {
        LocalFile localFile = new LocalFile();
        localFile.setFileName("error.json");
        fileService = new FileService(new HostedFile(recipeFileClient), localFile);

        RecipeService recipeService = new RecipeService(fileService);
        IngredientService ingredientService = new IngredientService(fileService);
        RecipeController recipeController = new RecipeController(recipeService,ingredientService);

        Assertions.assertThrows(NotFoundException.class, () -> {
            recipeController.getRecipes();
        });

    }
}

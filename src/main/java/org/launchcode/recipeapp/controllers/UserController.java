package org.launchcode.recipeapp.controllers;

import org.launchcode.recipeapp.models.Recipe;
import org.launchcode.recipeapp.models.Review;
import org.launchcode.recipeapp.models.User;
import org.launchcode.recipeapp.models.UserRecipe;
import org.launchcode.recipeapp.models.data.RecipeRepository;
import org.launchcode.recipeapp.models.data.UserRecipeRepository;
import org.launchcode.recipeapp.models.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Oksana
 */
@Controller
@RequestMapping("users")
public class UserController {

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private UserRecipeRepository userRecipeRepository;

   @Autowired
   private RecipeRepository recipeRepository;


   @GetMapping()
   public String getAllUsers(Model model) {
      List<User> users = new ArrayList<>();

      Iterable<User> usersIter = userRepository.findAll();


      usersIter.forEach(users::add);


      model.addAttribute("users", users);
      return "users/index";
   }

   @GetMapping("/profile")
   public String getUserProfile(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
      User sessionUser = (User) request.getSession().getAttribute("user");
      if (sessionUser == null) {
         model.addAttribute("title", "No user found");
      } else {
         List<Recipe> recipes = new ArrayList<>();
         List<UserRecipe> userRecipes = userRecipeRepository.getAllByUser(sessionUser);

         for (UserRecipe userRecipe : userRecipes) {
            Recipe recipe = userRecipe.getRecipe();
            recipes.add(recipe);
         }

         model.addAttribute("title", sessionUser.getUsername());
         model.addAttribute("user", sessionUser);
         model.addAttribute("recipes", recipes);
         model.addAttribute("title1", redirectAttributes.getAttribute("title1"));

      }
      return "users/profile";
   }

   @PostMapping("/addRecipe/{id}")
   public String addRecipe(@PathVariable Integer id, HttpServletRequest request, Model model,
                           RedirectAttributes redirectAttrs) {
      User sessionUser = (User) request.getSession().getAttribute("user");

      Optional<Recipe> recipeOptional = recipeRepository.findById(id);
      if (recipeOptional.isPresent()) {
         User user = userRepository.getById(sessionUser.getId());
         Recipe recipe = recipeOptional.get();


         UserRecipe userRecipe = new UserRecipe();
         userRecipe.setUser(user);
         userRecipe.setRecipe(recipe);

         Optional<UserRecipe> recipeByUserOptional = userRecipeRepository.findByRecipeAndUser(recipe,user);
         if (recipeByUserOptional.isPresent()) {
            return "redirect:/recipes/display?recipeId="+recipe.getId();
         } else {
            userRecipeRepository.save(userRecipe);
         }
      }

      return "redirect:/users/profile";
   }

   @PostMapping("/deleteRecipe/{id}")
   public String deleteRecipeFromFavorite(@PathVariable Integer id, HttpServletRequest request, Model model) {
      User sessionUser = (User) request.getSession().getAttribute("user");
      User user = userRepository.getById(sessionUser.getId());
      List<UserRecipe> userRecipes = userRecipeRepository.getAllByUser(user);

      Optional<Recipe> recipeOptional = recipeRepository.findById(id);
      if (recipeOptional.isPresent()) {

         Recipe recipe = recipeOptional.get();


         for (UserRecipe userRecipe : userRecipes) {
            Optional<Recipe> recipeOpt = recipeRepository.findById(userRecipe.getRecipe().getId());
            if (recipeOpt.isPresent()) {
               Recipe recipe1 = recipeOpt.get();
               if (recipe1.getId() == recipe.getId()) {

                  userRecipeRepository.delete(userRecipe);

               }
            }
         }
      }

      return "redirect:/users/profile";
   }
}






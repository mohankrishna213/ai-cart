package org.techm.samples.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.techm.samples.controller.api.CategoriesController;
import org.techm.samples.entity.Categories;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.service.auth.JwtService;
import org.techm.samples.service.categories.CategoriesService;
import org.techm.samples.service.categories.CategoriesServiceImpl;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriesController.class)   // <-- your real controller class
class CategoriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean                                 // <-- make Spring inject this mock
    private CategoriesServiceImpl categoriesService;
    
    @MockBean
    private JwtService jwtService;      // satisfies the JwtAuthFilter

    // if your filter needs more beans (e.g. UserDetailsService), mock them here too:
    @MockBean
    private UserDetailsService uds;

    @Test
    @WithMockUser
    void getAllCategoriesReturnsList() throws Exception {
        Categories c1 = new Categories();
        c1.setId(1L);
        c1.setName("Electronics");

        Mockito.when(categoriesService.getAllCategories())
               .thenReturn(List.of(c1));

        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name", is("Electronics")));
    }

    @Test
    @WithMockUser
    void getCategoryByIdFound() throws Exception {
        Categories c = new Categories();
        c.setId(55L);
        c.setName("Books");

        Mockito.when(categoriesService.getCategoryById(55L))
               .thenReturn(c);

        mockMvc.perform(get("/api/categories/55"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(55)))
               .andExpect(jsonPath("$.name", is("Books")));
    }

    @Test
    @WithMockUser
    void getCategoryByIdNotFound() throws Exception {
        Mockito.when(categoriesService.getCategoryById(99L))
               .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/categories/99"))
               .andExpect(status().isNotFound())
               .andExpect(content().string(containsString("Not found")));
    }
}

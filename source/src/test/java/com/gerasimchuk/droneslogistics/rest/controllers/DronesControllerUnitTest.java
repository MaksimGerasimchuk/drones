package com.gerasimchuk.droneslogistics.rest.controllers;

import com.gerasimchuk.droneslogistics.service.api.DroneService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Here is an example of mock mvc test.
 */
@WebMvcTest(controllers = DronesController.class, useDefaultFilters = false)
@Import(DronesController.class)
class DronesControllerUnitTest {

    private static final String BASE_URL = "/v1.0/drones";
    private static final String PAGE_NUMBER_PARAM_NAME = "pageNumber";
    private static final String PAGE_SIZE_PARAM_NAME = "pageSize";


    @MockBean
    private DroneService droneService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenGetDronesPageThenResultPageReturned() throws Exception {
        // given
        Mockito.when(droneService.getDronesPage(Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());

        //when
        mockMvc.perform(get(BASE_URL)
                .queryParam(PAGE_NUMBER_PARAM_NAME, "0")
                .queryParam(PAGE_SIZE_PARAM_NAME, "10"))
                //then
                .andExpect(status().isOk())
                .andReturn();

        // here we should also check actual response
        // but for serializing string into Page object we need to write some hack
        // which is not a purpose of this task
    }
}

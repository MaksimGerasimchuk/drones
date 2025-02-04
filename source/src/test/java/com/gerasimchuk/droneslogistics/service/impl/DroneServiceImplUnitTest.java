package com.gerasimchuk.droneslogistics.service.impl;

import com.gerasimchuk.droneslogistics.domain.repository.CargoRepository;
import com.gerasimchuk.droneslogistics.domain.repository.DroneBatteryCapacityRepository;
import com.gerasimchuk.droneslogistics.domain.repository.DroneRepository;
import com.gerasimchuk.droneslogistics.service.api.DroneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Here is an example of spring-based unit test.
 */
@ExtendWith(SpringExtension.class)
@Import(DroneServiceImpl.class)
class DroneServiceImplUnitTest {

    @MockBean
    private DroneRepository droneRepository;
    @MockBean
    private CargoRepository cargoRepository;
    @MockBean
    private DroneBatteryCapacityRepository capacityRepository;

    @Autowired
    private DroneService droneService;


    @Test
    void whenGetDronesPageThenDronesPageReturned(){
        // given
        Mockito.when(droneRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());

        //when
        var result = droneService.getDronesPage(PageRequest.of(0, 10));

        //then
        assertTrue(result.getContent().isEmpty());
    }
}

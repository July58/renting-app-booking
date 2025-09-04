package org.example.bookingrent;


import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.dapr.utils.TypeRef;
import org.example.bookingrent.TestcontainersConfiguration;
import org.example.bookingrent.dto.RentItem;
import org.example.bookingrent.req_res.ApiResponse;
import org.example.bookingrent.service.ExternalServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = {MockConfig.class, TestcontainersConfiguration.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ExternalServiceClientIntegrationTest {

    @Autowired
    private DaprClient daprClient;

    @Autowired
    private ExternalServiceClient externalServiceClient;

    private static final TypeRef<ApiResponse<RentItem>> RENT_ITEM_TYPE_REF =
            new TypeRef<ApiResponse<RentItem>>() {};

    @Test
    void checkAvailability_returnsRentItem_whenEnoughQuantity() {
        RentItem rentItem = new RentItem();
        rentItem.setId("item1");
        rentItem.setAmountOfCurrent(5);

        ApiResponse<RentItem> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentItem);

        doReturn(Mono.just(apiResponse))
                .when(daprClient)
                .invokeMethod(
                        eq("rentable-service"),
                        anyString(),
                        ArgumentMatchers.<Object>nullable(Object.class),
                        eq(HttpExtension.GET),
                        anyMap(),
                        ArgumentMatchers.<TypeRef<?>>any()
                );

        RentItem result = externalServiceClient.checkAvailability("item1", 2);

        assertNotNull(result);
        assertEquals("item1", result.getId());
        assertEquals(5, result.getAmountOfCurrent());
    }

    @Test
    void checkAvailability_returnsNull_whenNotEnoughQuantity() {
        RentItem rentItem = new RentItem();
        rentItem.setId("item1");
        rentItem.setAmountOfCurrent(1);

        ApiResponse<RentItem> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentItem);

        doReturn(Mono.just(apiResponse))
                .when(daprClient)
                .invokeMethod(
                        eq("rentable-service"),
                        anyString(),
                        ArgumentMatchers.<Object>nullable(Object.class),
                        eq(HttpExtension.GET),
                        anyMap(),
                        ArgumentMatchers.<TypeRef<?>>any()
                );

        RentItem result = externalServiceClient.checkAvailability("item1", 2);

        assertNull(result);
    }
}

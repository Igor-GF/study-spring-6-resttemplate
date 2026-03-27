package guru.springframework.spring7resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring7resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring7resttemplate.model.BeerDTO;
import guru.springframework.spring7resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring7resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    BeerClient beerClient;
    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder  restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO beerDto;
    String dtoJson;

    @Test
    void test_list_beers_with_query_param() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient.listBeers("ALE", null, null, null, null);
        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        beerDto = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(beerDto);
    }

    @Test
    void test_delete_not_found() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        beerDto.getId()))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(beerDto.getId());
        });
    }

    @Test
    void test_delete_beer() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        beerDto.getId()))
                .andRespond(withNoContent());

        beerClient.deleteBeer(beerDto.getId());

        server.verify();
    }

    @Test
    void test_update_beer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        beerDto.getId()))
                .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO responseDto = beerClient.updateBeer(beerDto);
        assertThat(responseDto.getId()).isEqualTo(beerDto.getId());
    }

    @Test
    void test_create_beer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH)
                .build(beerDto.getId());

        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO responseDto = beerClient.createBeer(beerDto);
        assertThat(responseDto.getId()).isEqualTo(beerDto.getId());
    }

    @Test
    void get_beer_by_id() {
        mockGetOperation();

        BeerDTO responseDto = beerClient.getBeerById(beerDto.getId());
        assertThat(responseDto.getId()).isEqualTo(beerDto.getId());
    }

    @Test
    void test_list_beer() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    BeerDTOPageImpl getPage(){
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.GET_BEER_BY_ID_PATH, beerDto.getId()))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }
}

package guru.springframework.spring7resttemplate.client;

import guru.springframework.spring7resttemplate.model.BeerDTO;
import org.springframework.data.domain.Page;

/**
 * Created by jt, Spring Framework Guru.
 */
public interface BeerClient {
    Page<BeerDTO> listBeers();

//    Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
//                            Integer pageNumber,Integer pageSize);
//
//    BeerDTO getBeerById(UUID beerId);
//
//    BeerDTO createBeer(BeerDTO newDto);
//
//    BeerDTO updateBeer(BeerDTO beerDto);
//
//    void deleteBeer(UUID beerId);
}

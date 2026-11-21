/* 
    Controller, passes each web request to the service.
*/
package server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// @RestController Tells Spring that this class handles HTTP requests and returns data (serialized into JSON)
// @RequestMapping defines the common URL prefix for all the endpoints in this controller. 
@RestController
@RequestMapping("/api")
public class TaxifyController {
    // Reference to SimulationService, which controls the flow of the simulation and retreives data wrt the simulation  
    private final SimulationService simulationService;
    public TaxifyController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return simulationService.status();
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        return simulationService.state();
    }

    @GetMapping("/step")
    public Map<String, Object> step() {
        simulationService.step();
        return Map.of("ok", true);
    }

    @GetMapping("/start")
    public Map<String, Object> start(@RequestParam(defaultValue = "1000") long ms) {
        simulationService.start(ms);
        return Map.of("running", true);
    }

    @GetMapping("/pause")
    public Map<String, Object> pause() {
        simulationService.pause();
        return Map.of("running", false);
    }
}
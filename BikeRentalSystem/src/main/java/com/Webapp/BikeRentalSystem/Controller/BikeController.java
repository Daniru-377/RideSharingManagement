package com.Webapp.BikeRentalSystem.Controller;


import com.Webapp.BikeRentalSystem.DSA.BikeSort;
import com.Webapp.BikeRentalSystem.Model.Bike;
import com.Webapp.BikeRentalSystem.Service.BikeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/bikes")
public class BikeController {

    private final BikeService service;

    public BikeController(BikeService service) {
        this.service = service;
    }


    @GetMapping
    public String showBikes(@RequestParam(value = "sort", required = false) String sort,
                            @RequestParam(value = "message", required = false) String message,
                            Model model) throws IOException {

        List<Bike> bikes = service.getAllBikes();

        if ("availability".equals(sort)) {
            BikeSort.quickSort(bikes, 0, bikes.size() - 1);
        }

        model.addAttribute("bikes", bikes);
        model.addAttribute("message", message);
        return "bikes/bikes";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) throws IOException {
        model.addAttribute("nextId", service.getNextBikeId());
        return "bikes/addBike";
    }


    @PostMapping("/add")
    public String addBike(@RequestParam("bikeId") String bikeId,
                          @RequestParam("model") String bikeModel,
                          @RequestParam("type") String type,
                          @RequestParam("location") String location,
                          @RequestParam("pricePerDay") double pricePerDay,
                          @RequestParam("available") boolean available,
                          @RequestParam("status") String status,
                          Model model,
                          RedirectAttributes redirectAttributes) throws IOException {

        Bike bike = new Bike(bikeId, bikeModel, type, location, pricePerDay, available, status);
        String result = service.addBike(bike);

        if (result.equals("OK")) {
            redirectAttributes.addAttribute("message", "Bike added successfully");
            return "redirect:/bikes";
        }


        model.addAttribute("error", result);
        model.addAttribute("nextId", bikeId);
        return "bikes/addBike";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) throws IOException {
        Bike bike = service.getBikeById(id);

        if (bike == null) {
            return "redirect:/bikes";
        }

        model.addAttribute("bike", bike);
        return "bikes/editBike";
    }


    @PostMapping("/edit")
    public String updateBike(@RequestParam("bikeId") String bikeId,
                             @RequestParam("model") String bikeModel,
                             @RequestParam("type") String type,
                             @RequestParam("location") String location,
                             @RequestParam("pricePerDay") double pricePerDay,
                             @RequestParam("available") boolean available,
                             @RequestParam("status") String status,
                             Model model,
                             RedirectAttributes redirectAttributes) throws IOException {

        Bike bike = new Bike(bikeId, bikeModel, type, location, pricePerDay, available, status);
        String result = service.updateBike(bike);

        if (result.equals("OK")) {
            redirectAttributes.addAttribute("message", "Bike updated successfully");
            return "redirect:/bikes";
        }

        model.addAttribute("error", result);
        model.addAttribute("bike", bike);
        return "bikes/editBike";
    }


    @PostMapping("/delete")
    public String deleteBike(@RequestParam("bikeId") String bikeId,
                             RedirectAttributes redirectAttributes) throws IOException {

        String result = service.deleteBike(bikeId);

        if (result.equals("OK")) {
            redirectAttributes.addAttribute("message", "Bike deleted successfully");
        } else {
            redirectAttributes.addAttribute("message", result);
        }
        return "redirect:/bikes";
    }
}
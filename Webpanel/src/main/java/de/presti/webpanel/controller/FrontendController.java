package de.presti.webpanel.controller;

import de.presti.webpanel.sql.repository.URLEntryService;
import de.presti.webpanel.utils.GoogleImageSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.server.ServerRequest;

@Controller
public class FrontendController {

    @Autowired
    private URLEntryService urlEntryService;

    @GetMapping("/")
    public String main(Model model, @RequestParam(name = "choice", defaultValue = "dream") String choice) {
        if (!choice.equalsIgnoreCase("dream") && !choice.equalsIgnoreCase("george") && !choice.equalsIgnoreCase("sapnap") && !choice.equalsIgnoreCase("tommy")) {
            choice = "dream";
        }

        model.addAttribute("choice", choice);
        model.addAttribute("image", GoogleImageSearcher.searchForImage(choice));

        return "index";
    }
}

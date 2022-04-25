package de.presti.webpanel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FrontendController {

    @RequestMapping("/")
    public String main(Model model, @RequestParam(name = "choice", defaultValue = "dream") String choice) {
        if (!choice.equalsIgnoreCase("dream") && !choice.equalsIgnoreCase("george") && !choice.equalsIgnoreCase("sapnap") && !choice.equalsIgnoreCase("tommy")) {
            choice = "dream";
        }

        model.addAttribute("choice", choice);
        model.addAttribute("image", GoogleImageSearcher.searchForImage(choice));
        return "index";
    }

}

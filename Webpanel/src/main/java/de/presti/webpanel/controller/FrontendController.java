package de.presti.webpanel.controller;

import de.presti.webpanel.WebpanelApplication;
import de.presti.webpanel.utils.GoogleImageSearcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String getMain(Model model, @RequestParam(name = "choice", defaultValue = "dream") String choice) {
        if (!choice.equalsIgnoreCase("dream") && !choice.equalsIgnoreCase("george") && !choice.equalsIgnoreCase("sapnap") && !choice.equalsIgnoreCase("tommy")) {
            choice = "dream";
        }

        model.addAttribute("choice", choice);
        model.addAttribute("image", GoogleImageSearcher.searchForImage(choice));
        model.addAttribute("userCount", WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().getEntryCount());
        model.addAttribute("entryCount", WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().getEntryCount());

        return "index";
    }

    @PostMapping("/")
    public String postMain(Model model, HttpServletResponse response,
                           HttpServletRequest request) {
        String choice = request.getParameter("choice");
        String imageUrl = request.getParameter("imageUrl");

        if (imageUrl != null && choice != null) {
            if (request.getParameter("nobutton") != null) WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().addEntry(imageUrl, "NO");
            if (request.getParameter("yesbutton") != null) WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().addEntry(imageUrl, choice);
        } else if (choice == null) {
            choice = "dream";
        }

        model.addAttribute("choice", choice);
        model.addAttribute("image", GoogleImageSearcher.searchForImage(choice));
        model.addAttribute("userCount", WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().getEntryCount());
        model.addAttribute("entryCount", WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().getEntryCount());

        return "index";
    }
}

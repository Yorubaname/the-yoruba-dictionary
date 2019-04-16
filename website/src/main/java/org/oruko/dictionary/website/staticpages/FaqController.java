package org.oruko.dictionary.website.staticpages;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for about us page
 */

@Controller
public class FaqController {

    @RequestMapping("/faqs")
    public String aboutUsIndexPage(Model map) {
        map.addAttribute("title", "Frequent Asked Questions - FAQs");
        return "faq";
    }
}

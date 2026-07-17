package org.obscura.backend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Client-side routes (added for the game detail page) don't correspond to a
 * real static file, so a full page load/refresh on one would 404 without
 * this — it forwards to index.html so the SPA's router can take over.
 */
@Controller
public class SpaForwardingController {

    @RequestMapping("/games/{appId}")
    public String gameDetail() {
        return "forward:/index.html";
    }
}

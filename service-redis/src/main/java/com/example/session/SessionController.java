package com.example.session;

import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Request;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session")
public class SessionController {

    @PostMapping("/save")
    public String saveSessionData(HttpSession session, @RequestParam String key, @RequestParam String value) {
        session.setAttribute(key, value);
        return "Saved key: " + key + ", value: " + value + " in session.";
    }

    @GetMapping("/get")
    public String getSessionData(HttpSession session, @RequestParam String key) {
        Object value = session.getAttribute(key);
        return value != null ? "key: " + key + ", value: " + value : "key not found.";
    }

    @PostMapping("/invalidate")
    public String invalidateSession(HttpSession session) {
        session.invalidate();
        return "Session invalidated.";
    }
}

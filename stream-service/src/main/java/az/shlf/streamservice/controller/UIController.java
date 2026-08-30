package az.shlf.streamservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

   @GetMapping("/ws-ui/index")
   public String getWebSocketUI() {
      // "static" qovluğundakı index.html faylına yönləndirir
      return "forward:/index.html";
   }
}
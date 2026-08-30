package az.shlf.streamservice.model.enums;

import lombok.Getter;

@Getter
public enum SessionPrefix {

   ROOM("ROOM:"),
   USER("USER:");

   private final String prefix;

   SessionPrefix(String prefix) {
      this.prefix = prefix;
   }
}
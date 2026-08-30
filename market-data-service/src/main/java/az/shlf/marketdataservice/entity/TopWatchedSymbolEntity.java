package az.shlf.marketdataservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "top_watched_symbols")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopWatchedSymbolEntity {

   @Id
   @Column(name = "symbol", length = 20)
   private String symbol;

   @Column(name = "name", length = 50)
   private String name;

   @Column(name = "watch_count", nullable = false)
   private Long watchCount;
}
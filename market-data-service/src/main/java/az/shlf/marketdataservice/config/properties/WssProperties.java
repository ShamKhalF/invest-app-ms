package az.shlf.marketdataservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "binance.wss")
@Getter
@Setter
public class WssProperties {
    private String baseUrl;
    private String singlePath;
    private String multiPath;
    private String klineSinglePath;
    private String klineMultiPath;
    private String depthSinglePath;
    private String depthMultiPath;
}

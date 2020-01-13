import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

@Data
public abstract class Driver {

    private String currentPageSource;
    private Document currentPageDom;
    private String currentActivity;


    public List<Map<String, Object>> getListFromXPath(String key) {
        if (key.startsWith("/") || key.startsWith("(")) {
//            XpathUtil
            // todo
        }

        return null;
    }

}

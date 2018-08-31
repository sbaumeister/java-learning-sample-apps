package io.github.sbaumeister.productcrawler.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.OpenFoodFactsClient;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.Product;
import org.springframework.beans.factory.annotation.Autowired;

@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
public class StartView extends VerticalLayout {

    private Button searchButton;
    private TextField gtinTextField;
    private ProgressBar progressBar;
    private OpenFoodFactsClient openFoodFactsClient;

    @Autowired
    public StartView(OpenFoodFactsClient openFoodFactsClient) {
        this.openFoodFactsClient = openFoodFactsClient;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setId("start-view");
        setClassName("app-view");

        H1 heading = new H1("Product Data Crawler");
        add(heading);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        gtinTextField = new TextField();
        gtinTextField.setId("search-input");
        gtinTextField.setMaxLength(13);
        gtinTextField.setPlaceholder("Enter a GTIN/EAN...");

        progressBar = new ProgressBar();

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        UI ui = attachEvent.getUI();
        addAsyncSearchButtonClickListener(ui);

        horizontalLayout.add(gtinTextField, searchButton);

        add(horizontalLayout);

        add(progressBar);
    }

    private void addAsyncSearchButtonClickListener(UI ui) {
        searchButton.addClickListener(event -> {
            String gtin = gtinTextField.getValue();
            Thread thread = createFetchDataThread(ui, gtin);
            thread.start();
        });
    }

    private Thread createFetchDataThread(UI ui, String gtin) {
        return new Thread(() -> {
            ui.access(() -> progressBar.setIndeterminate(true));

            Product product = openFoodFactsClient.getProduct(gtin);

            ui.access(() -> {
                addSearchItem(product);
                progressBar.setIndeterminate(false);
            });
        });
    }

    private void addSearchItem(Product product) {
        Div searchItemDiv = new Div();
        searchItemDiv.setClassName("product-item");
        H3 productNameHeading = new H3(product.getProductName());
        searchItemDiv.add(productNameHeading);
        searchItemDiv.add(new Image(product.getImageNutritionUrl(), "No product image available"));
        add(searchItemDiv);
    }
}

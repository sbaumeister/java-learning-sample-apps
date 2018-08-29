package io.github.sbaumeister.productcrawler.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
import io.github.sbaumeister.productcrawler.clients.openfoodfacts.OpenFoodFactsClient;
import io.github.sbaumeister.productcrawler.clients.openfoodfacts.Product;

@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
public class AppComponent extends VerticalLayout {

    private Button searchButton;
    private TextField gtinTextField;
    private ProgressBar progressBar;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setId("app");

        H1 heading = new H1("Product Data Crawler");
        add(heading);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        gtinTextField = new TextField();
        gtinTextField.setMaxLength(13);
        gtinTextField.setPlaceholder("Enter a GTIN/EAN...");

        progressBar = new ProgressBar();

        searchButton = new Button("Search");
        UI ui = attachEvent.getUI();
        addAsyncSearchButtonClickListener(ui);

        horizontalLayout.add(gtinTextField, searchButton);

        add(horizontalLayout);

        add(progressBar);
    }

    private void addAsyncSearchButtonClickListener(UI ui) {
        OpenFoodFactsClient openFoodFactsClient = new OpenFoodFactsClient();
        searchButton.addClickListener(event -> {
            String gtin = gtinTextField.getValue();
            Thread thread = createFetchDataThread(ui, gtin, openFoodFactsClient);
            thread.start();
        });
    }

    private Thread createFetchDataThread(UI ui, String gtin, OpenFoodFactsClient openFoodFactsClient) {
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
        H3 productNameHeading = new H3(product.getProductName());
        searchItemDiv.add(productNameHeading);
        searchItemDiv.add(new Image(product.getImageNutritionUrl(), "No product image available"));
        add(searchItemDiv);
    }
}

package io.github.sbaumeister.productcrawler.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.OpenFoodFactsClient;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
public class StartView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger LOG = LoggerFactory.getLogger(StartView.class);

    private Button searchButton;
    private TextField gtinTextField;
    private ProgressBar progressBar;
    private OpenFoodFactsClient openFoodFactsClient;
    private String gtinParam;
    private Notification productNotFoundNotification;

    @Autowired
    public StartView(OpenFoodFactsClient openFoodFactsClient) {
        this.openFoodFactsClient = openFoodFactsClient;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        gtinParam = parameter;
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
        searchButton.addClickListener(event -> startAsyncSearch(ui));

        horizontalLayout.add(gtinTextField, searchButton);

        add(horizontalLayout);

        add(progressBar);

        productNotFoundNotification = new Notification("Product not found", 3000, Position.MIDDLE);

        if (gtinParam != null) {
            gtinTextField.setValue(gtinParam);
            startAsyncSearch(ui);
        }
    }

    private void startAsyncSearch(UI ui) {
        Thread thread = createFetchDataThread(ui);
        thread.start();
    }

    private Thread createFetchDataThread(UI ui) {
        String gtin = gtinTextField.getValue();
        return new Thread(() -> {
            ui.access(() -> progressBar.setIndeterminate(true));

            Optional<Product> optionalProduct = openFoodFactsClient.getProduct(gtin);

            ui.access(() -> {
                if (optionalProduct.isPresent() && optionalProduct.get().getCode().length() > 0) {
                    addSearchItem(optionalProduct.get());
                } else {
                    productNotFoundNotification.open();
                }
                progressBar.setIndeterminate(false);
            });
        });
    }

    private void addSearchItem(Product product) {
        Div searchItem = new Div();
        searchItem.setClassName("product-item");
        H3 productNameHeading = new H3(product.getProductName());
        searchItem.add(productNameHeading);
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Image image = new Image(product.getImageSmallUrl(), "No product image available");
        horizontalLayout.add(image);
        FormLayout formLayout = new FormLayout();
        TextField codeField = new TextField("EAN/GTIN", product.getCode(), "");
        codeField.setReadOnly(true);
        TextField brandsField = new TextField("Brands", product.getBrands(), "");
        brandsField.setReadOnly(true);
        formLayout.add(codeField, brandsField);
        horizontalLayout.add(formLayout);
        searchItem.add(horizontalLayout);
//        add(searchItem);
        getElement().insertChild(3, searchItem.getElement());
    }
}

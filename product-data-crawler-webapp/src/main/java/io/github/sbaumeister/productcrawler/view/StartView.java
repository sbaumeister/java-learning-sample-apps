package io.github.sbaumeister.productcrawler.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
public class StartView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger LOG = LoggerFactory.getLogger(StartView.class);

    private Button searchButton;
    private TextField searchTextField;
    private ProgressBar progressBar;
    private OpenFoodFactsClient openFoodFactsClient;
    private String searchTermParam;
    private Notification productNotFoundNotification;
    private Div searchResultContainer;

    @Autowired
    public StartView(OpenFoodFactsClient openFoodFactsClient) {
        this.openFoodFactsClient = openFoodFactsClient;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        searchTermParam = parameter;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setId("start-view");
        setClassName("app-view");
        UI ui = attachEvent.getUI();

        H1 heading = new H1("Product Data Crawler");
        add(heading);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        searchTextField = new TextField();
        searchTextField.setId("search-input");
        searchTextField.setMaxLength(13);
        searchTextField.setPlaceholder("Enter a GTIN/EAN or product name...");
        searchTextField.addKeyPressListener(event -> {
            if (event.getKey().matches("Enter")) {
                startAsyncSearch(ui);
            }
        });

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        searchButton.addClickListener(event -> startAsyncSearch(ui));

        horizontalLayout.add(searchTextField, searchButton);
        horizontalLayout.setWidth("100%");
        horizontalLayout.setFlexGrow(0, searchButton);
        horizontalLayout.setFlexGrow(1, searchTextField);

        add(horizontalLayout);

        progressBar = new ProgressBar();
        add(progressBar);

        searchResultContainer = new Div();
        searchResultContainer.setId("search-result-container");
        add(searchResultContainer);

        productNotFoundNotification = new Notification("Product not found", 3000, Position.MIDDLE);

        if (searchTermParam != null) {
            searchTextField.setValue(searchTermParam);
            startAsyncSearch(ui);
        }
    }

    private void startAsyncSearch(UI ui) {
        Thread thread = createFetchDataThread(ui);
        thread.start();
    }

    private Thread createFetchDataThread(UI ui) {
        String searchTerm = searchTextField.getValue();
        return new Thread(() -> {
            ui.access(() -> progressBar.setIndeterminate(true));

            List<Product> foundProducts = new ArrayList<>();
            if (searchTerm.matches("[0-9]{13}")) {
                Optional<Product> optionalProduct = openFoodFactsClient.getProductByGtin(searchTerm);
                if (optionalProduct.isPresent()) {
                    foundProducts.add(optionalProduct.get());
                }
            } else {
                foundProducts.addAll(openFoodFactsClient.getProductsByName(searchTerm));
            }

            ui.access(() -> {
                if (foundProducts.size() > 0) {
                    addSearchResultItems(foundProducts);
                } else {
                    productNotFoundNotification.open();
                }
                progressBar.setIndeterminate(false);
            });
        });
    }

    private void addSearchResultItems(List<Product> foundProducts) {
        searchResultContainer.removeAll();
        for (Product foundProduct : foundProducts) {
            Component searchResultItem = createSearchResultItem(foundProduct);
            searchResultContainer.add(searchResultItem);
        }
    }

    private Component createSearchResultItem(Product product) {
        Div searchResultItem = new Div();
        searchResultItem.setClassName("search-result-item");
        H3 productNameHeading = new H3(product.getProductName());
        searchResultItem.add(productNameHeading);
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        if (product.getImageSmallUrl() != null) {
            Image image = new Image(product.getImageSmallUrl(), "No product image available");
            horizontalLayout.add(image);
        }
        FormLayout formLayout = new FormLayout();
        TextField codeField = new TextField("EAN/GTIN", product.getCode(), "");
        codeField.setReadOnly(true);
        TextField brandsField = new TextField("Brands", product.getBrands(), "");
        brandsField.setReadOnly(true);
        formLayout.add(codeField, brandsField);
        horizontalLayout.add(formLayout);
        searchResultItem.add(horizontalLayout);
        return searchResultItem;
    }
}

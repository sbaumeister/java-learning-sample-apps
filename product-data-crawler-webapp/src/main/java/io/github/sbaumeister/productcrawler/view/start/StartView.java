package io.github.sbaumeister.productcrawler.view.start;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.OpenFoodFactsClient;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
public class StartView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger LOG = LoggerFactory.getLogger(StartView.class);

    private Button searchButton;
    private ComboBox<String> searchCombo;
    private ProgressBar progressBar;
    private OpenFoodFactsClient openFoodFactsClient;
    private JdbcTemplate jdbcTemplate;
    private String searchTermParam;
    private Notification productNotFoundNotification;
    private Div searchResultContainer;

    @Autowired
    public StartView(OpenFoodFactsClient openFoodFactsClient, JdbcTemplate jdbcTemplate) {
        this.openFoodFactsClient = openFoodFactsClient;
        this.jdbcTemplate = jdbcTemplate;
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
        searchCombo = new ComboBox<>();
        searchCombo.setId("search-input");
        searchCombo.setPlaceholder("Enter a GTIN/EAN or product name...");
        searchCombo.setDataProvider(new CallbackDataProvider<String, Void>(query -> {
            return fetchLastSerachTermsFromDb().stream();
        }, query -> {
            return fetchLastSerachTermsFromDb().size();
        }));
        searchCombo.setAllowCustomValue(true);
        searchCombo.getElement().addEventListener("keyup", event -> {
            startAsyncSearch(ui);
        }).setFilter("event.key == 'Enter'");

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        searchButton.addClickListener(event -> startAsyncSearch(ui));

        horizontalLayout.add(searchCombo, searchButton);
        horizontalLayout.setWidth("100%");
        horizontalLayout.setFlexGrow(0, searchButton);
        horizontalLayout.setFlexGrow(1, searchCombo);

        add(horizontalLayout);

        progressBar = new ProgressBar();
        add(progressBar);

        searchResultContainer = new Div();
        searchResultContainer.setId("search-result-container");
        add(searchResultContainer);

        productNotFoundNotification = new Notification("Product not found", 3000, Position.MIDDLE);

        if (searchTermParam != null) {
            searchCombo.getElement().setProperty("value", searchTermParam);
            startAsyncSearch(ui);
        }
    }

    private List<String> fetchLastSerachTermsFromDb() {
        return jdbcTemplate.queryForList("SELECT SEARCH_TERM FROM SEARCH_QUERIES ORDER BY CREATED_AT DESC",
                String.class);
    }

    private void startAsyncSearch(UI ui) {
        String searchTerm = searchCombo.getElement().getProperty("value");
        if (searchTerm != null) {
            Thread thread = createFetchDataThread(ui, searchTerm);
            thread.start();
        }
    }

    private Thread createFetchDataThread(UI ui, String searchTerm) {
        return new Thread(() -> {
            ui.access(() -> progressBar.setIndeterminate(true));

            insertSearchTermIntoDb(searchTerm);

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
                searchCombo.getDataProvider().refreshAll();
                if (foundProducts.size() > 0) {
                    addSearchResultItems(foundProducts);
                } else {
                    productNotFoundNotification.open();
                }
                progressBar.setIndeterminate(false);
            });
        });
    }

    private void insertSearchTermIntoDb(String searchTerm) {
        jdbcTemplate.update("INSERT INTO SEARCH_QUERIES (SEARCH_TERM) VALUES (?)", searchTerm);
    }

    private void addSearchResultItems(List<Product> foundProducts) {
        searchResultContainer.removeAll();
        for (Product foundProduct : foundProducts) {
            Component searchResultItem = new SearchResultItem(foundProduct);
            searchResultContainer.add(searchResultItem);
        }
    }
}

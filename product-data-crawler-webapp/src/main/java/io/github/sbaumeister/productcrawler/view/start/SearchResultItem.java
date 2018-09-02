package io.github.sbaumeister.productcrawler.view.start;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.github.sbaumeister.productcrawler.service.openfoodfacts.Product;

class SearchResultItem extends Div {

    SearchResultItem(Product product) {
        setClassName("search-result-item");
        H3 productNameHeading = new H3(product.getProductName());
        add(productNameHeading);
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
        add(horizontalLayout);
    }
}

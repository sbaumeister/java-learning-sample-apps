package io.github.sbaumeister.productcrawler.service.openfoodfacts;

import java.util.List;

class NameSearchResult {

    private List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}

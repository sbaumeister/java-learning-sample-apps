package io.github.sbaumeister.productcrawler.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.dom.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppComponentTest {

    @Test
    public void test() {
        AppComponent appComponent = new AppComponent();
        ComponentUtil.onComponentAttach(appComponent, true);
        appComponent.onAttach(new AttachEvent(appComponent, true));

        Element element = appComponent.getElement();
    }
}
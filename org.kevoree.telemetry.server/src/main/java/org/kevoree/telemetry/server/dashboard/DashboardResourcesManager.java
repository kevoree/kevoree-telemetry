package org.kevoree.telemetry.server.dashboard;

import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by gregory.nain on 27/08/2014.
 */
public class DashboardResourcesManager implements ResourceManager {
    @Override
    public Resource getResource(String s) throws IOException {
        System.out.println("Get Resource:" + s);
        try {
            System.out.println(getClass().getClassLoader().getResource("static/" + s).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        InputStream stream = getClass().getClassLoader().getResourceAsStream("static/" + s);
        try {
            System.out.println(getClass().getClassLoader().getResource("static/" + s).toURI().toString());
            FileResourceManager mgr = new FileResourceManager(new File(getClass().getClassLoader().getResource("/static").toURI()),1000);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener resourceChangeListener) {

    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener resourceChangeListener) {

    }

    @Override
    public void close() throws IOException {

    }
}

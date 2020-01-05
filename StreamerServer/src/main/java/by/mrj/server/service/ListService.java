package by.mrj.server.service;

import java.util.Collection;

public interface ListService {

    void remove(String listName, Collection<?> ids);

    void add(String listName, Collection<?> ids);
}

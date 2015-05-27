package io.selendroid.server.model.internal;

import io.selendroid.server.model.AndroidElement;

import java.util.List;

public interface FindsByTsId {
  AndroidElement findElementByTsId(String using);

  List<AndroidElement> findElementsByTsId(String using);
}

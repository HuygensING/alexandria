package nl.knaw.huygens.alexandria.dropwizard.api;

/*
 * #%L
 * alexandria-markup-lmnl-server
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.markup.api.DocumentInfo;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class DocumentService {

  Set<UUID> uuids = new LinkedHashSet<>();
  private static String baseURI;
  private final TAGStore store;

  public DocumentService(ServerConfiguration config) {
    baseURI = config.getBaseURI();
    store = config.getStore();
  }

  static Cache<UUID, TAGDocument> documentCache = CacheBuilder.newBuilder()//
      .maximumSize(100)//
      .build();

  public Optional<DocumentWrapper> getDocument(UUID uuid) {
    if (uuids.contains(uuid)) {
      try {
        DocumentWrapper document = new DocumentWrapper(store, documentCache.get(uuid, readDocument(uuid)));
        return Optional.of(document);
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private static Callable<? extends TAGDocument> readDocument(UUID uuid) {
    return () -> null;
  }

  public void setDocument(UUID docId, DocumentWrapper document) {
    documentCache.put(docId, document.getDocument());

    DocumentInfo docInfo = getDocumentInfo(docId)//
        .orElseGet(() -> newDocumentInfo(docId));
    docInfo.setModified(Instant.now());
    documentInfoCache.put(docId, docInfo);

    uuids.add(docId);
  }

  static Cache<UUID, DocumentInfo> documentInfoCache = CacheBuilder.newBuilder()//
      .maximumSize(100)//
      .build();

  public Optional<DocumentInfo> getDocumentInfo(UUID uuid) {
    if (uuids.contains(uuid)) {
      try {
        DocumentInfo documentInfo = documentInfoCache.get(uuid, () -> null);
        return Optional.ofNullable(documentInfo);
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private static Callable<? extends DocumentInfo> readDocumentInfo(UUID uuid) {
    return () -> newDocumentInfo(uuid);
  }

  private static DocumentInfo newDocumentInfo(UUID uuid) {
    return new DocumentInfo(uuid, baseURI)//
        .setCreated(Instant.now())//
        .setModified(Instant.now());
  }

  public Collection<UUID> getDocumentUUIDs() {
    return uuids;
  }

}

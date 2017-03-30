package nl.knaw.huygens.alexandria.dropwizard.api;

/*
 * #%L
 * alexandria-markup-lmnl-server
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.lmnl.data_model.Document;
import nl.knaw.huygens.alexandria.markup.api.DocumentInfo;

public class DocumentService {

  Set<UUID> uuids = new LinkedHashSet<>();
  private static String baseURI;

  public DocumentService(ServerConfiguration config) {
    baseURI = config.getBaseURI();
  }

  static Cache<UUID, Document> documentCache = CacheBuilder.newBuilder()//
      .maximumSize(100)//
      .build();

  public Optional<Document> getDocument(UUID uuid) {
    if (uuids.contains(uuid)) {
      try {
        Document document = documentCache.get(uuid, readDocument(uuid));
        return Optional.of(document);
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private static Callable<? extends Document> readDocument(UUID uuid) {
    return () -> null;
  }

  public void setDocument(UUID docId, Document document) {
    documentCache.put(docId, document);

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

package nl.knaw.huygens.alexandria.dropwizard.api;

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

package nl.knaw.huygens.alexandria.dropwizard.api;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2019 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;

import java.util.Optional;

public class NamedDocumentService {
  private final TAGStore store;

  public NamedDocumentService(TAGStore store) {
    this.store = store;
  }

  static final Cache<String, Long> documentIdCache =
      CacheBuilder.newBuilder().maximumSize(100).build();

  public void registerDocument(TAGDocument document, String docName) {
    documentIdCache.put(docName, document.getDbId());
  }

  public Optional<TAGDocument> getDocumentByName(String docName) {
    Long docId = documentIdCache.getIfPresent(docName);
    if (docId == null) {
      return Optional.empty();
    } else {
      TAGDocument document = store.getDocument(docId);
      if (document == null) {
        return Optional.empty();
      }
      return Optional.of(document);
    }
  }
}

package com.marklogic.samplestack.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.extra.jackson.JacksonHandle;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.ValuesHandle;
import com.marklogic.client.query.CountedDistinctValue;
import com.marklogic.client.query.DeleteQueryDefinition;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.RawCombinedQueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.client.query.ValuesDefinition;
import com.marklogic.samplestack.SampleStackException;
import com.marklogic.samplestack.service.MarkLogicOperations;

@Component
public class MarkLogicClient implements MarkLogicOperations {


	private final Logger logger = LoggerFactory
			.getLogger(MarkLogicClient.class);

	@Autowired
	protected DatabaseClient client;

	@Override
	public JsonNode getJsonDocument(String uri) {
		JacksonHandle jacksonHandle = client.newJSONDocumentManager().read(
				uri, new JacksonHandle());
		return jacksonHandle.get();
	}

//	@Override
//	public void insertGraph(String graphIri, String mediaType,
//			InputStream inputStream) {
//		GraphManager graphManager = client.newGraphManager();
//		graphManager.insert(graphIri, Format.TURTLE, new InputStreamHandle(
//				inputStream));
//	}

//	@Override
//	public String sparql(String sparqlQuery) {
//		SPARQLManager sparqlManager = client.newSPARQLManager();
//		StringHandle queryHandle = new StringHandle(sparqlQuery)
//				.withFormat(Format.SPARQL_QUERY);
//		// TODO query handles should set format themselves. fix marker
//		// interfaces.
//		StringHandle responseHandle = new StringHandle()
//				.withFormat(Format.JSON);
//		responseHandle = sparqlManager.sparql(queryHandle, responseHandle);
//		return responseHandle.get();
//	}

	
	@Override
	public List<String> getDocumentUris(String directory) {
		ClassPathResource values1 = new ClassPathResource("doc-uris.json");
		try {
			FileHandle fileHandle = new FileHandle(values1.getFile())
					.withFormat(Format.JSON);
			QueryManager queryManager = client.newQueryManager();
			RawCombinedQueryDefinition qdef = queryManager
					.newRawCombinedQueryDefinition(fileHandle);
			ValuesDefinition valdef = queryManager.newValuesDefinition("docs");
			valdef.setQueryDefinition(qdef);
			ValuesHandle handle = client.newQueryManager().values(valdef,
					new ValuesHandle());

			List<String> docUrisList = new ArrayList<String>();
			for (CountedDistinctValue value : handle.getValues()) {
				String valueString = value.get("string", String.class);
				// TODO refactor to actually query properly.
				if (valueString.startsWith(directory)) {
					docUrisList.add(value.get("string", String.class));
				}
				else {}
			}
			return docUrisList;
		} catch (IOException e) {
			throw new SampleStackException(e);
		}
	}

	@Override
	//TODO refactor to use multipart search capability.
	public SearchHandle searchDirectory(String directory, String queryString) {
		QueryManager queryManager = client.newQueryManager();
		StructuredQueryBuilder qb = new StructuredQueryBuilder(directory.replaceAll("/", ""));
		StructuredQueryDefinition qdef = qb.term(queryString);
		qdef.setDirectory(directory);
		logger.debug(qdef.serialize());
		return queryManager.search(qdef, new SearchHandle());
	}

	@Override
	public void deleteDirectory(String directory) {
		QueryManager queryManager = client.newQueryManager();
		DeleteQueryDefinition deleteDef = queryManager.newDeleteDefinition();
		deleteDef.setDirectory(directory);
		queryManager.delete(deleteDef);
	}
	
	
	public JSONDocumentManager newJSONDocumentManager() {
		return client.newJSONDocumentManager();
	}

	@Override
	public SearchHandle search(QueryDefinition queryDefinition) {
		return search(queryDefinition, 1);
	}
	
	@Override
	public SearchHandle search(QueryDefinition queryDefinition, long start) {
		QueryManager queryManager = client.newQueryManager();
		return queryManager.search(queryDefinition, new SearchHandle(), start);
	}
}
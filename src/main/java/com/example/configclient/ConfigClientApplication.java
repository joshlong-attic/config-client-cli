package com.example.configclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.*;

@SpringBootApplication(proxyBeanMethods = false)
public class ConfigClientApplication {

	public static void main(String[] args) {
		var context = new SpringApplicationBuilder()
			.web(WebApplicationType.NONE)
			.sources(ConfigClientApplication.class)
			.run(args);
	}

	@Bean
	CommandLineRunner ready() {
		return args -> {
			var index = 0;
			var username = args[index++];
			var pw = args[index++];
			var springApplicationName = args[index++];
			var profile = args[index++];
			var configServerUri = args[index++];
			var outputPath = args[index++];

			var http = WebClient.builder()
				.filter(ExchangeFilterFunctions.basicAuthentication(username, pw))
				.build();
			var url = configServerUri + '/' + springApplicationName + '/' + profile;
			var map = http.get().uri(url).retrieve().bodyToMono(JsonNode.class)
				.map(body -> {
					var propertySources = (ArrayNode) body.get("propertySources");
					System.out.println("not sure what kind of propertySources i have now");
					var maps = new ArrayList<Map<String, String>>();
					propertySources.elements().forEachRemaining(json -> maps.add(mapOfConfigurationFrom(json)));
					Collections.reverse(maps);
					var toWriteOut = new HashMap<String, String>();
					maps.stream().forEachOrdered(toWriteOut::putAll);
					return toWriteOut;
				});
			var results = map.block();
			var existingContent = new StringBuilder();
			var file = new File(outputPath);
			try (var fr = new BufferedReader(new FileReader(file))) {
				existingContent.append(FileCopyUtils.copyToString(fr));
				existingContent.append(System.lineSeparator());
			}
			try (var fw = new BufferedWriter(new FileWriter(file))) {
				fw.write(existingContent.toString());
				Objects.requireNonNull(results).forEach((key, value) -> {
					var newLine = key + "=" + value + System.lineSeparator();
					try {
						fw.write(newLine);
					}
					catch (IOException e) {
						ReflectionUtils.rethrowRuntimeException(e);
					}
				});
			}
		};
	}

	private Map<String, String> mapOfConfigurationFrom(JsonNode jsonNode) {
		var m = new HashMap<String, String>();
		var source = (ObjectNode) jsonNode.get("source");
		source.fieldNames().forEachRemaining(fn -> m.put(fn, source.get(fn).textValue()));
		return m;
	}
}

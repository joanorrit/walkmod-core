/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
  Walkmod is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Walkmod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.conf.providers.yml;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class SetWriterYMLAction extends AbstractYMLConfigurationAction {

   private String chain;
   private String type;
   private String path;
   private Map<String, String> params;

   public SetWriterYMLAction(String chain, String type, String path, YAMLConfigurationProvider provider,
         boolean recursive, Map<String, String> params) {
      super(provider, recursive);
      this.chain = chain;
      this.type = type;
      this.path = path;
      this.params = params;
   }

   @Override
   public void doAction(JsonNode node) throws Exception {

      ObjectNode writer = null;

      ObjectMapper mapper = provider.getObjectMapper();

      if (node.has("chains") && (chain == null || "".equals(chain.trim()))) {
         chain = "default";
      }

      if (chain != null && !"".equals(chain.trim())) {

         if (node.has("chains")) {
            JsonNode chainsListNode = node.get("chains");
            if (chainsListNode.isArray()) {
               Iterator<JsonNode> it = chainsListNode.iterator();
               boolean found = false;
               while (it.hasNext() && !found) {
                  JsonNode current = it.next();
                  if (current.has("name")) {
                     String name = current.get("name").asText();
                     found = name.equals(chain);
                     if (found) {
                        if (current.has("writer")) {
                           writer = (ObjectNode) current.get("writer");
                        } else {
                           writer = new ObjectNode(mapper.getNodeFactory());
                        }

                        if (type != null && !"".equals(type.trim())) {
                           writer.set("type", new TextNode(type));
                        }
                        if (path != null && !"".equals(path.trim())) {
                           writer.set("path", new TextNode(path));
                        }
                        if (params != null && !params.isEmpty()) {
                           ObjectNode paramsObject = null;
                           if (writer.has("params")) {
                              paramsObject = (ObjectNode) writer.get("params");
                           } else {
                              paramsObject = new ObjectNode(mapper.getNodeFactory());
                              writer.set("params", paramsObject);
                           }

                           Set<String> keys = params.keySet();
                           for (String key : keys) {
                              paramsObject.put(key, params.get(key).toString());
                           }

                        }
                     }
                  }
               }
            }
         }
         if (writer != null) {
            provider.write(node);
         }
      } else {
         if (!node.has("chains")) {
            ArrayNode chains = new ArrayNode(mapper.getNodeFactory());
            ObjectNode defaultChain = new ObjectNode(mapper.getNodeFactory());
            defaultChain.set("name", new TextNode("default"));
            ObjectNode writerNode = new ObjectNode(mapper.getNodeFactory());

            if (type != null && !"".equals(type.trim())) {
               writerNode.set("type", new TextNode(type));
            }
            if (path != null && !"".equals(path.trim())) {
               writerNode.set("path", new TextNode(path));
            }
            if (params != null && !params.isEmpty()) {
               ObjectNode paramsObject = new ObjectNode(mapper.getNodeFactory());
               Set<String> keys = params.keySet();
               for (String key : keys) {
                  paramsObject.put(key, params.get(key).toString());
               }
               writerNode.set("params", paramsObject);
            }

            if (node.has("transformations")) {
               defaultChain.set("transformations", node.get("transformations"));
            }
            defaultChain.set("writer", writerNode);
            chains.add(defaultChain);
            ObjectNode root = new ObjectNode(mapper.getNodeFactory());

            root.set("chains", chains);
            provider.write(root);
         }
      }

   }

   @Override
   public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
      return new SetWriterYMLAction(chain, type, path, (YAMLConfigurationProvider) provider, recursive, params);
   }

}

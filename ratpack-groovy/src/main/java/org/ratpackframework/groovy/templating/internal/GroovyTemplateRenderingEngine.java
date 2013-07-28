/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.groovy.templating.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.buffer.ByteBuf;
import org.ratpackframework.groovy.script.internal.ScriptEngine;
import org.ratpackframework.groovy.templating.TemplatingConfig;
import org.ratpackframework.util.internal.Result;
import org.ratpackframework.util.internal.ResultAction;
import org.ratpackframework.util.internal.Transformer;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

public class GroovyTemplateRenderingEngine {

  private static final String ERROR_TEMPLATE = "error.html";

  private final LoadingCache<TemplateSource, CompiledTemplate> compiledTemplateCache;
  private final TemplateCompiler templateCompiler;
  private final boolean checkTimestamp;

  @Inject
  public GroovyTemplateRenderingEngine(TemplatingConfig templatingConfig) {
    ScriptEngine<TemplateScript> scriptEngine = new ScriptEngine<TemplateScript>(getClass().getClassLoader(), templatingConfig.isStaticallyCompile(), TemplateScript.class);
    templateCompiler = new TemplateCompiler(scriptEngine);
    this.compiledTemplateCache = CacheBuilder.newBuilder().maximumSize(templatingConfig.getCacheSize()).build(new CacheLoader<TemplateSource, CompiledTemplate>() {
      @Override
      public CompiledTemplate load(TemplateSource templateSource) throws Exception {
        return templateCompiler.compile(templateSource.getContent(), templateSource.getName());
      }
    });

    checkTimestamp = templatingConfig.isCheckTimestamp();
  }

  public void renderTemplate(final File templateDir, final String templateId, final Map<String, ?> model, final ResultAction<ByteBuf> handler) {
    final File templateFile = getTemplateFile(templateDir, templateId);
    render(templateDir, new FileTemplateSource(templateFile, templateId, checkTimestamp), model, handler);
  }

  public void renderError(final File templateDir, Map<String, ?> model, ResultAction<ByteBuf> handler) {
    final File errorTemplate = getTemplateFile(templateDir, ERROR_TEMPLATE);
    if (errorTemplate.exists()) {
      render(templateDir, new FileTemplateSource(errorTemplate, ERROR_TEMPLATE, checkTimestamp), model, handler);
    } else {
      render(templateDir, new ResourceTemplateSource(ERROR_TEMPLATE), model, handler);
    }
  }

  private void render(final File templateDir, final TemplateSource templateSource, Map<String, ?> model, ResultAction<ByteBuf> handler) {
    try {
      new Render(compiledTemplateCache, templateSource, model, handler, new Transformer<String, TemplateSource>() {
        public TemplateSource transform(String templateName) {
          return new FileTemplateSource(new File(templateDir, templateName), templateName, checkTimestamp);
        }
      });
    } catch (Exception e) {
      handler.execute(new Result<ByteBuf>(e));
    }
  }

  private File getTemplateFile(File templateDir, String templateName) {
    return new File(templateDir, templateName);
  }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.file.impl;

import static org.apache.commons.collections.map.LazyMap.decorate;
import static org.apache.commons.lang.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang.ClassUtils.getAllSuperclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileType;

import org.apache.commons.collections.Factory;

// TODO: Cache lookups / build one massive structure
public class DataflowPersistenceHandlerRegistry {
	private static final MapFactory MAP_FACTORY = new MapFactory();
	private static final SetFactory SET_FACTORY = new SetFactory();

	@SuppressWarnings("unchecked")
	protected static List<Class<?>> findAllParentClasses(
			final Class<?> sourceClass) {
		List<Class<?>> superClasses = new ArrayList<>();
		superClasses.add(sourceClass);
		superClasses.addAll(getAllSuperclasses(sourceClass));
		superClasses.addAll(getAllInterfaces(sourceClass));
		return superClasses;
	}

	private Map<Class<?>, Set<DataflowPersistenceHandler>> openClassToHandlers;
	private Map<Class<?>, Set<FileType>> openClassToTypes;
	private Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> openFileClassToHandler;
	private Map<FileType, Set<DataflowPersistenceHandler>> openFileToHandler;
	private Map<Class<?>, Set<DataflowPersistenceHandler>> saveClassToHandlers;
	private Map<Class<?>, Set<FileType>> saveClassToTypes;
	private Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> saveFileClassToHandler;
	private Map<FileType, Set<DataflowPersistenceHandler>> saveFileToHandler;

	private List<DataflowPersistenceHandler> dataflowPersistenceHandlers;

	public DataflowPersistenceHandlerRegistry() {
	}

	public Set<FileType> getOpenFileTypes() {
		return getOpenFileClassToHandler().keySet();
	}

	public Set<FileType> getOpenFileTypesFor(Class<?> sourceClass) {
		Set<FileType> fileTypes = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass))
			fileTypes.addAll(getOpenClassToTypes().get(candidateClass));
		return fileTypes;
	}

	public Set<DataflowPersistenceHandler> getOpenHandlersFor(
			Class<? extends Object> sourceClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass))
			handlers.addAll(getOpenClassToHandlers().get(candidateClass));
		return handlers;
	}

	public Set<DataflowPersistenceHandler> getOpenHandlersFor(
			FileType fileType, Class<? extends Object> sourceClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass))
			handlers.addAll(getOpenFileClassToHandler().get(fileType).get(
					candidateClass));
		return handlers;
	}

	public Set<DataflowPersistenceHandler> getOpenHandlersForType(
			FileType fileType) {
		return getOpenFileToHandler().get(fileType);
	}

	public synchronized Set<DataflowPersistenceHandler> getOpenHandlersForType(
			FileType fileType, Class<?> sourceClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass))
			handlers.addAll(getOpenFileClassToHandler().get(fileType).get(
					candidateClass));
		return handlers;
	}

	public Set<FileType> getSaveFileTypes() {
		return getSaveFileClassToHandler().keySet();
	}

	public Set<FileType> getSaveFileTypesFor(Class<?> destinationClass) {
		Set<FileType> fileTypes = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(destinationClass))
			fileTypes.addAll(getSaveClassToTypes().get(candidateClass));
		return fileTypes;
	}

	public Set<DataflowPersistenceHandler> getSaveHandlersFor(
			Class<? extends Object> destinationClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(destinationClass))
			handlers.addAll(getSaveClassToHandlers().get(candidateClass));
		return handlers;
	}

	public Set<DataflowPersistenceHandler> getSaveHandlersForType(
			FileType fileType, Class<?> destinationClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<>();
		for (Class<?> candidateClass : findAllParentClasses(destinationClass))
			handlers.addAll(getSaveFileClassToHandler().get(fileType).get(
					candidateClass));
		return handlers;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void createCollections() {
		openFileClassToHandler = decorate(new HashMap(), MAP_FACTORY);
		openFileToHandler = decorate(new HashMap(), SET_FACTORY);
		openClassToTypes = decorate(new HashMap(), SET_FACTORY);
		openClassToHandlers = decorate(new HashMap(), SET_FACTORY);

		saveFileClassToHandler = decorate(new HashMap(), MAP_FACTORY);
		saveFileToHandler = decorate(new HashMap(), SET_FACTORY);
		saveClassToTypes = decorate(new HashMap(), SET_FACTORY);
		saveClassToHandlers = decorate(new HashMap(), SET_FACTORY);
	}

	private Map<Class<?>, Set<DataflowPersistenceHandler>> getOpenClassToHandlers() {
		return openClassToHandlers;
	}

	private synchronized Map<Class<?>, Set<FileType>> getOpenClassToTypes() {
		return openClassToTypes;
	}

	private synchronized Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> getOpenFileClassToHandler() {
		return openFileClassToHandler;
	}

	private Map<FileType, Set<DataflowPersistenceHandler>> getOpenFileToHandler() {
		return openFileToHandler;
	}

	private Map<Class<?>, Set<DataflowPersistenceHandler>> getSaveClassToHandlers() {
		return saveClassToHandlers;
	}

	private synchronized Map<Class<?>, Set<FileType>> getSaveClassToTypes() {
		return saveClassToTypes;
	}

	private synchronized Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> getSaveFileClassToHandler() {
		return saveFileClassToHandler;
	}

	/**
	 * Bind method for SpringDM.
	 * 
	 * @param service
	 * @param properties
	 */
	public void update(Object service, Map<?, ?> properties) {
		if (dataflowPersistenceHandlers != null)
			updateColletions();
	}

	public synchronized void updateColletions() {
		createCollections();
		for (DataflowPersistenceHandler handler : dataflowPersistenceHandlers) {
			for (FileType openFileType : handler.getOpenFileTypes()) {
				Set<DataflowPersistenceHandler> set = openFileToHandler
						.get(openFileType);
				set.add(handler);
				for (Class<?> openClass : handler.getOpenSourceTypes()) {
					openFileClassToHandler.get(openFileType).get(openClass)
							.add(handler);
					openClassToTypes.get(openClass).add(openFileType);
				}
			}
			for (Class<?> openClass : handler.getOpenSourceTypes())
				openClassToHandlers.get(openClass).add(handler);

			for (FileType saveFileType : handler.getSaveFileTypes()) {
				saveFileToHandler.get(saveFileType).add(handler);
				for (Class<?> saveClass : handler.getSaveDestinationTypes()) {
					saveFileClassToHandler.get(saveFileType).get(saveClass)
							.add(handler);
					saveClassToTypes.get(saveClass).add(saveFileType);
				}
			}
			for (Class<?> openClass : handler.getSaveDestinationTypes())
				saveClassToHandlers.get(openClass).add(handler);
		}
	}

	public void setDataflowPersistenceHandlers(
			List<DataflowPersistenceHandler> dataflowPersistenceHandlers) {
		this.dataflowPersistenceHandlers = dataflowPersistenceHandlers;
	}

	private static class MapFactory implements Factory {
		@Override
		@SuppressWarnings("rawtypes")
		public Object create() {
			return decorate(new HashMap(), SET_FACTORY);
		}
	}

	private static class SetFactory implements Factory {
		@Override
		public Object create() {
			return new LinkedHashSet<Object>();
		}
	}
}

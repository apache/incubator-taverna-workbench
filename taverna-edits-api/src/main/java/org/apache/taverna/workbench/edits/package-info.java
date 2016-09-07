/**
 * A {@link net.sf.taverna.t2.workbench.edits.EditManager} that can manage
 * {@link net.sf.taverna.t2.workflowmodel.Edit}s performed from the UI.
 * <p>
 * To perform an edit that is to be undoable, use
 * {@link EditManager#doDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow, net.sf.taverna.t2.workflowmodel.Edit)}
 * instead of {@link net.sf.taverna.t2.workflowmodel.Edit#doEdit()}. Such edits
 * can be
 * {@link EditManager#undoDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow) undone}
 * and
 * {@link EditManager#redoDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow) redone}.
 * </p>
 * <p>
 * Edits are organised by {@link net.sf.taverna.t2.workflowmodel.Dataflow} so
 * that if a user changes the active workflow in the Workbench and does "Undo" -
 * that would undo the last undo done related to that workflow.
 * </p>
 * <p>
 * The {@link net.sf.taverna.t2.workbench.edits.impl} implementation of the
 * EditManager is discovered by {@link net.sf.taverna.t2.workbench.edits.EditManager#getInstance()}. The
 * implementation also includes {@link net.sf.taverna.t2.ui.menu.MenuComponent}s
 * for Undo and Redo.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
package org.apache.taverna.workbench.edits;
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

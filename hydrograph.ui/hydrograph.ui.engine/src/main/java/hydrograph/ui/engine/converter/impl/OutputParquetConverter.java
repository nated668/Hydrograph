/*******************************************************************************
 * Copyright 2017 Capital One Services, LLC and Bitwise, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package hydrograph.ui.engine.converter.impl;

import hydrograph.engine.jaxb.commontypes.TypeBaseField;
import hydrograph.engine.jaxb.commontypes.TypeOutputInSocket;
import hydrograph.engine.jaxb.ofparquet.TypeOutputDelimitedInSocket;
import hydrograph.engine.jaxb.outputtypes.ParquetFile;
import hydrograph.ui.common.util.Constants;
import hydrograph.ui.datastructure.property.GridRow;
import hydrograph.ui.engine.constants.PropertyNameConstants;
import hydrograph.ui.engine.converter.OutputConverter;
import hydrograph.ui.graph.model.Component;
import hydrograph.ui.graph.model.Link;
import hydrograph.ui.logging.factory.LogFactory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class OutputParquetConverter extends OutputConverter {

	private static final Logger logger = LogFactory.INSTANCE.getLogger(OutputParquetConverter.class);

	public OutputParquetConverter(Component component) {
		super(component);
		this.component = component;
		this.properties = component.getProperties();
		this.baseComponent = new ParquetFile();
	}

	@Override
	public void prepareForXML() {
		logger.debug("Generating XML data for {}", properties.get(Constants.PARAM_NAME));
		super.prepareForXML();

		ParquetFile parquetFile = (ParquetFile) baseComponent;

		ParquetFile.Path path = new ParquetFile.Path();
		path.setUri((String) properties.get(PropertyNameConstants.PATH.value()));

		parquetFile.setPath(path);
		parquetFile.setRuntimeProperties(getRuntimeProperties());
		parquetFile.setOverWrite(getTrueFalse(PropertyNameConstants.OVER_WRITE.value()));
	}

	@Override
	protected List<TypeOutputInSocket> getOutInSocket() {
		logger.debug("Generating TypeOutputInSocket data");
		List<TypeOutputInSocket> outputinSockets = new ArrayList<>();
		for (Link link : component.getTargetConnections()) {
			TypeOutputDelimitedInSocket outInSocket = new TypeOutputDelimitedInSocket();
			outInSocket.setId(link.getTargetTerminal());
			if (converterHelper.isMultipleLinkAllowed(link.getSource(), link.getSourceTerminal()))
				outInSocket.setFromSocketId(link.getSource().getPort(link.getSourceTerminal()).getPortType()
						+ link.getLinkNumber());
			else
				outInSocket.setFromSocketId(link.getSourceTerminal());
			outInSocket.setFromSocketType(link.getSource().getPorts().get(link.getSourceTerminal()).getPortType());
			outInSocket.setType(link.getTarget().getPort(link.getTargetTerminal()).getPortType());
			outInSocket.setSchema(getSchema());
			outInSocket.getOtherAttributes();
			outInSocket.setFromComponentId(link.getSource().getComponentId());
			outputinSockets.add(outInSocket);
		}
		return outputinSockets;
	}

	@Override
	protected List<TypeBaseField> getFieldOrRecord(List<GridRow> gridRowList) {
		logger.debug("Generating data for {} for property {}", new Object[] { properties.get(Constants.PARAM_NAME),
				PropertyNameConstants.SCHEMA.value() });

		List<TypeBaseField> typeBaseFields = new ArrayList<>();
		if (gridRowList != null && gridRowList.size() != 0) {
			for (GridRow object : gridRowList)
				typeBaseFields.add(converterHelper.getSchemaGridTargetData(object));

		}
		return typeBaseFields;
	}
}

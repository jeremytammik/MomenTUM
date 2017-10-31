/*******************************************************************************
 * Welcome to the pedestrian simulation framework MomenTUM. 
 * This file belongs to the MomenTUM version 2.0.2.
 * 
 * This software was developed under the lead of Dr. Peter M. Kielar at the
 * Chair of Computational Modeling and Simulation at the Technical University Munich.
 * 
 * All rights reserved. Copyright (C) 2017.
 * 
 * Contact: peter.kielar@tum.de, https://www.cms.bgu.tum.de/en/
 * 
 * Permission is hereby granted, free of charge, to use and/or copy this software
 * for non-commercial research and education purposes if the authors of this
 * software and their research papers are properly cited.
 * For citation information visit:
 * https://www.cms.bgu.tum.de/en/31-forschung/projekte/456-momentum
 * 
 * However, further rights are not granted.
 * If you need another license or specific rights, contact us!
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package tum.cms.sim.momentum.model.output.writerSources.specificWriterSources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import tum.cms.sim.momentum.data.layout.ScenarioManager;
import tum.cms.sim.momentum.infrastructure.execute.SimulationState;
import tum.cms.sim.momentum.model.output.writerSources.genericWriterSources.SingleElementWriterSource;
import tum.cms.sim.momentum.utility.generic.Unique;
import tum.cms.sim.momentum.utility.lattice.CellIndex;
import tum.cms.sim.momentum.utility.lattice.ILattice;
import tum.cms.sim.momentum.utility.spaceSyntax.DepthMap;
import tum.cms.sim.momentum.utility.spaceSyntax.SpaceSyntax;

/**
 * This writer source class writes a single data element which is
 * generated by annotations in the Space Syntax classes. It is essential
 * for the export of the Space Syntax calculation results.
 * @author Christian Thieme
 *
 */
public class SpaceSyntaxWriterSource extends SingleElementWriterSource {
	
	private XStream xStream = null;
	private ScenarioManager scenarioManager = null;
	private SpaceSyntax spaceSyntaxResult = null;
	private Integer additionalId = null;
	
	public void setScenarioManager(ScenarioManager scenarioManager) {
		this.scenarioManager = scenarioManager;
	}
	
	public void setAdditionalId(Integer additionalId) {
		this.additionalId = additionalId;
	}

	@Override
	public String readSingleValue(String outputTypeName) {

		String xml;
		xml = this.xStream.toXML(this.spaceSyntaxResult);
		
		return xml;
	}

	@Override
	public void initialize(SimulationState simulationState) {
		
		this.spaceSyntaxResult = this.scenarioManager.getSpaceSyntaxes()
				.stream()
				.filter(spaceSyntaxRes -> spaceSyntaxRes.getId().intValue() == this.additionalId.intValue())
				.findFirst().get();
		
		this.xStream = new XStream();
		this.configureXStream(this.xStream, spaceSyntaxResult.getlattice());
		
		this.dataItemNames.add("adding fake key");
	}

	/**
	 * This method processes necessary annotations for SpaceSyntax results. Furthermore 
	 * a converter for the CellIndex class is registered. 
	 * 
	 * Developer note: This method need to supplement the CellIndex with an according 
	 * value from the lattice/grid. This is because CellIndex class stores the indices 
	 * in a generic class (Pair<Integer, Integer>) which can not be handeled properly 
	 * by XStream.
	 * 
	 * @param xStream an instance of xStream that is used to convert the object to a string
	 * @param lattice
	 */
	private void configureXStream(XStream xStream, ILattice lattice) {

		xStream.processAnnotations(SpaceSyntax.class);
		xStream.processAnnotations(DepthMap.class);
		
		xStream.alias("CellIndex", CellIndex.class);
		xStream.omitField(CellIndex.class, "index");
		
		xStream.useAttributeFor(Unique.class, "id");
		xStream.useAttributeFor(Unique.class, "name");
		 
		xStream.registerConverter(new ReflectionConverter(xStream.getMapper(), xStream.getReflectionProvider()) {
			
			@SuppressWarnings("rawtypes")
			@Override
			public boolean canConvert(Class clazz) {

				return clazz.equals(CellIndex.class);
			}
			
			@Override
			public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
				
				CellIndex cellIndex = (CellIndex) source;
				
				writer.addAttribute("y", "" + cellIndex.getRow());
				writer.addAttribute("x", "" + cellIndex.getColumn());
				writer.addAttribute("value", "" + lattice.getCellNumberValue(cellIndex));
				writer.close();
				
			}
		}, 5000);
	}
}

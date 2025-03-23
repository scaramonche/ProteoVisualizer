# Proteo Visualizer App for Cytoscape

## Description

In high-throughput mass spectrometry (MS), proteins are digested into peptides and the peptide MS signals are then used to infer protein relative quantities across samples. Proteins that cannot be unambiguously distinguished based on the available set of peptides are reported as protein groups containing several protein accessions. The Cytoscape app **Proteo Visualizer** complements the widely used [stringApp](https://apps.cytoscape.org/apps/stringApp) by creating STRING networks from protein groups input instead of single protein accessions. In the resulting networks, each protein group is represented as a single node that inherits all existing edges of the group members. In addition, all relevant node and edge attributes are aggregated. 

![app_overview](/figures/Fig_App_Overview.png)

First, the user provides a list of protein groups instead of single accessions as input to the network query. Then, the app uses this list to retrieve a STRING network via stringApp and to create a collapsed group node for each protein group in the resulting network. Thereby, all node and edge attributes are automatically aggregated such that group nodes as well as edges connecting group nodes represent an average of the information in the protein groups. The final network has the look and feel of a STRING network and is compatible with stringApp’s functionality such as functional enrichment. In addition, users can uncollapse any group of their choice and explore the node information of the individual members, which show up as separate rows in the Cytoscape Node table. As a result of the edge aggregation strategy, some protein group edges can have confidence scores that are lower than the confidence cutoff specified when retrieving the network and thus are represented as dashed edge lines. 

Proteo Visualizer networks are designed to be compatible with other Cytoscape apps such as [clusterMaker](https://apps.cytoscape.org/apps/clustermaker2) and [Omics Visualizer](https://apps.cytoscape.org/apps/OmicsVisualizer), thus allowing users to perform typical downstream analysis tasks while keeping the full resolution of the proteomic groups detected in MS-based proteomics. Although the current version only supports STRING networks, future versions of the app will be agnostic to the source of the network and work on user-generated data such as affinity-purification followed by mass spectrometry (AP-MS) or networks retrieved from interaction databases such as IntAct or GeneMania. The app is available for download at the Cytoscape App Store: https://apps.cytoscape.org/apps/ProteoVisualizer.

## Implementation

Proteo Visualizer relies on the Cytoscape stringApp for retrieving networks from the STRING database as well as the built-in Cytoscape CyGroups functionality for the creation and maintenance of the group nodes. In accordance with other Cytoscape apps, the main functionality is also available via commands and can be automatically executed from R or Python via the Cytoscape automation interface.

## Citation

Locard-Paulet M\*, Doncheva NT\*, Morris JH and Jensen LJ (2024). Functional analysis of MS-based proteomics data: from protein groups to networks. *Molecular and Cellular Proteomics*, **23**:100871.  
[Abstract](https://pubmed.ncbi.nlm.nih.gov/39486590/) [Full text](https://doi.org/10.1016/j.mcpro.2024.100871)  

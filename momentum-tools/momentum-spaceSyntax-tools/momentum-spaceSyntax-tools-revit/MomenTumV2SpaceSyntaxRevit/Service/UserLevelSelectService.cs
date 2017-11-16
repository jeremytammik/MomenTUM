﻿using Autodesk.Revit.DB;
using Autodesk.Revit.UI;
using MomenTumV2SpaceSyntaxRevit.View;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MomenTumV2SpaceSyntaxRevit.Service
{
    class UserLevelSelectService
    {
        public static Level LevelSelectedByUser { private get; set; }

        public static KeyValuePair<Result, Level> LetUserPickLevelFromDialog(Document doc)
        {
            FilteredElementCollector levelCollector = new FilteredElementCollector(doc);
            ICollection<Element> levelCollection = levelCollector.OfClass(typeof(Level)).ToElements();

            var levels = new List<Level>();
            foreach (Element element in levelCollection)
            {
                Level level = element as Level;
                if (level != null)
                {
                    levels.Add(level);
                }
            }

            if (levels.Count == 0)
            {
                PromtService.DisplayErrorToUser("The project does not contain any levels.");
                return new KeyValuePair<Result, Level>(Result.Failed, null);
            }

            return OpenLevelSelector(levels);
        }

        private static KeyValuePair<Result, Level> OpenLevelSelector(List<Level> levels)
        {
            var levelSelectorDialog = new LevelSelectorHost();
            levelSelectorDialog.InitializeLevelListBox(levels);

            levelSelectorDialog.ShowDialog();

            if (LevelSelectedByUser == null)
            {
                PromtService.DisplayInformationToUser("Operation cancelled by User.");
                return new KeyValuePair<Result, Level>(Result.Cancelled, null);
            }

            return new KeyValuePair<Result, Level>(Result.Succeeded, LevelSelectedByUser);
        }
    }
}

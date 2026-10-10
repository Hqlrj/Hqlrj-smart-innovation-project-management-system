-- 检查项目39是否有计划书
SELECT * FROM project_plan WHERE project_id = 39;

-- 检查项目39的planFilePath
SELECT id, project_name, plan_file_path, plan_file_name FROM project WHERE id = 39;
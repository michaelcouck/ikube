select * from action as a join rule as r on a.id = r.action_id where a.result = 1 order by a.id desc ;

select a.actionName, a.indexName, a.working, a.duration, a.result, r.name, r.result from action as a, rule as r 
    where a.id = r.action_id and a.result = 1 order by a.id desc;
-- and a.result = 1

-- delete from rule;
-- delete from action;
-- commit;
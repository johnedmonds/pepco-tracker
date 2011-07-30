select 'DC' as area, whengenerated as observed, dcaffectedcustomers as outages from summary UNION
select 'Prince George' as area, whengenerated as observed, pgaffectedcustomers as outages from summary UNION
select 'Montgomery' as area, whengenerated as observed, montaffectedcustomers as outages from summary

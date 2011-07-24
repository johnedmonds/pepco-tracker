select whengenerated as observed, dcaffectedcustomers as outages, 'DC' as area from summary UNION
select whengenerated as observed, pgaffectedcustomers as outages, 'Prince George' as area from summary UNION
select whengenerated as observed, montaffectedcustomers as outages, 'Montgomery' as area from summary

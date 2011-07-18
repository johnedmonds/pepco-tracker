library(ggplot2)

reliability <- read.csv(stdin())
reliability$observed <- as.Date(reliability$observed, format="%Y-%m-%d %H:%M:%S")

areas <- factor(reliability$area)
reliability_plot <- ggplot(reliability, aes(x=observed, y=outages))
reliability_plot <- reliability_plot + geom_area(aes(fill=areas))
#reliability_plot <- reliability_plot + scale_fill_manual()
ggsave(reliability_plot,filename='reliability.png')

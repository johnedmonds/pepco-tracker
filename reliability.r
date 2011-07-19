library(ggplot2)

reliability <- read.csv('/dev/stdin')
reliability$observed <- as.Date(reliability$observed, format="%Y-%m-%d %H:%M:%S")

areas <- factor(reliability$area)
#Make the plot.
reliability_plot <- ggplot(reliability, aes(x=observed, y=outages))
reliability_plot <- reliability_plot + geom_area(aes(fill=areas))
#Remove the grey padding.
reliability_plot <- reliability_plot + scale_x_date(expand=c(0,0))
reliability_plot <- reliability_plot + scale_y_continuous(expand=c(0,0))
#reliability_plot <- reliability_plot + opts(legend.position=c(.5,.5))
#reliability_plot <- reliability_plot + scale_fill_manual()
reliability_dpi<-72
ggsave(reliability_plot,filename='reliability.svg', width=900/reliability_dpi, height=150/reliability_dpi, dpi=reliability_dpi)

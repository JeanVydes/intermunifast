import { FunctionComponent } from 'preact';
import { ArrowUp, ArrowDown, TrendingUp } from 'lucide-preact';

export interface StatCardProps {
    title: string;
    value: string | number;
    change?: number;
    changeLabel?: string;
    trend?: 'up' | 'down' | 'neutral';
    icon?: any;
    sparklineData?: number[];
}

export const StatCard: FunctionComponent<StatCardProps> = ({
    title,
    value,
    change,
    changeLabel = 'compared to last week',
    trend = 'neutral',
    icon: Icon,
    sparklineData
}) => {
    const getTrendColor = () => {
        if (trend === 'up') return 'text-green-600';
        if (trend === 'down') return 'text-red-600';
        return 'text-gray-600';
    };

    const getTrendIcon = () => {
        if (trend === 'up') return <ArrowUp className="w-4 h-4" />;
        if (trend === 'down') return <ArrowDown className="w-4 h-4" />;
        return null;
    };

    return (
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-medium text-gray-600">{title}</h3>
                {Icon && (
                    <div className="p-2 bg-purple-50 rounded-lg">
                        <Icon className="w-5 h-5 text-purple-600" />
                    </div>
                )}
            </div>

            <div className="flex items-end justify-between">
                <div>
                    <p className="text-3xl font-bold text-gray-900">{value}</p>
                    {change !== undefined && (
                        <div className="flex items-center gap-1 mt-2">
                            <span className={`flex items-center gap-1 text-sm font-medium ${getTrendColor()}`}>
                                {getTrendIcon()}
                                {change > 0 ? '+' : ''}{change}%
                            </span>
                            <span className="text-sm text-gray-500">{changeLabel}</span>
                        </div>
                    )}
                </div>

                {sparklineData && (
                    <div className="h-12 w-24">
                        <SimplesparkLine data={sparklineData} color={trend === 'up' ? '#16a34a' : trend === 'down' ? '#dc2626' : '#6b7280'} />
                    </div>
                )}
            </div>
        </div>
    );
};

// Simple SVG sparkline component
const SimplesparkLine: FunctionComponent<{ data: number[]; color: string }> = ({ data, color }) => {
    if (!data || data.length === 0) return null;

    const max = Math.max(...data);
    const min = Math.min(...data);
    const range = max - min || 1;

    const points = data.map((value, index) => {
        const x = (index / (data.length - 1)) * 100;
        const y = 100 - ((value - min) / range) * 100;
        return `${x},${y}`;
    }).join(' ');

    return (
        <svg viewBox="0 0 100 100" className="w-full h-full" preserveAspectRatio="none">
            <polyline
                fill="none"
                stroke={color}
                strokeWidth="3"
                points={points}
            />
        </svg>
    );
};

export default StatCard;
